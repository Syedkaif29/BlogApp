import { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { blogAPI } from '../services/api';
import { AuthContext } from '../contexts/AuthContext';
import LoadingSpinner from '../components/LoadingSpinner';

const EditBlogPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

  const [blog, setBlog] = useState(null);
  const [formData, setFormData] = useState({
    title: '',
    content: ''
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errors, setErrors] = useState({});

  useEffect(() => {
    const fetchBlog = async () => {
      try {
        setLoading(true);
        const response = await blogAPI.getBlog(id);
        setBlog(response);
        setFormData({
          title: response.title,
          content: response.content
        });

        // Check if user is the author
        console.log('EditBlog - User:', user);
        console.log('EditBlog - Blog response:', response);
        console.log('EditBlog - User ID:', user?.id);
        console.log('EditBlog - Blog Author ID:', response.author?.id);
        
        if (user && user.id !== response.author?.id) {
          setErrors({
            authorization: 'You are not authorized to edit this blog post'
          });
        }
      } catch (error) {
        setErrors({
          fetch: error.message || 'Failed to load blog post'
        });
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchBlog();
    }
  }, [id, user]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));

    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.title.trim()) {
      newErrors.title = 'Title is required';
    } else if (formData.title.trim().length < 3) {
      newErrors.title = 'Title must be at least 3 characters long';
    }

    if (!formData.content.trim()) {
      newErrors.content = 'Content is required';
    } else if (formData.content.trim().length < 10) {
      newErrors.content = 'Content must be at least 10 characters long';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    try {
      setSaving(true);
      await blogAPI.updateBlog(id, {
        title: formData.title.trim(),
        content: formData.content.trim()
      });

      // Navigate back to the blog post
      navigate(`/blog/${id}`, { replace: true });
    } catch (error) {
      setErrors({
        submit: error.message || 'Failed to update blog post'
      });
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    navigate(`/blog/${id}`, { replace: true });
  };

  // Show loading spinner while fetching
  if (loading) {
    return (
      <div className="max-w-4xl mx-auto">
        <LoadingSpinner size="lg" className="py-12" />
      </div>
    );
  }

  // Show error if failed to fetch or unauthorized
  if (errors.fetch || errors.authorization) {
    return (
      <div className="max-w-4xl mx-auto">
        <div className="bg-red-50 border border-red-200 rounded-md p-4">
          <p className="text-red-800">{errors.fetch || errors.authorization}</p>
          <Link
            to="/"
            className="mt-2 inline-block text-blue-600 hover:text-blue-800 underline"
          >
            ← Back to blogs
          </Link>
        </div>
      </div>
    );
  }

  // Check if user is authorized to edit
  const isAuthor = user && blog && user.id === blog.author?.id;
  if (!isAuthor) {
    return (
      <div className="max-w-4xl mx-auto">
        <div className="bg-red-50 border border-red-200 rounded-md p-4">
          <p className="text-red-800">You are not authorized to edit this blog post</p>
          <Link
            to={`/blog/${id}`}
            className="mt-2 inline-block text-blue-600 hover:text-blue-800 underline"
          >
            ← Back to blog post
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto">
      {/* Navigation */}
      <div className="mb-6">
        <Link
          to={`/blog/${id}`}
          className="text-blue-600 hover:text-blue-800 font-medium"
        >
          ← Back to blog post
        </Link>
      </div>

      <div className="bg-white rounded-lg shadow-md p-6 lg:p-8">
        <h1 className="text-3xl font-bold text-gray-800 mb-8">Edit Blog Post</h1>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Title field */}
          <div>
            <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-2">
              Title *
            </label>
            <input
              type="text"
              id="title"
              name="title"
              value={formData.title}
              onChange={handleChange}
              className={`w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${errors.title ? 'border-red-500' : 'border-gray-300'
                }`}
              placeholder="Enter your blog post title"
              disabled={saving}
            />
            {errors.title && (
              <p className="mt-1 text-sm text-red-600">{errors.title}</p>
            )}
          </div>

          {/* Content field */}
          <div>
            <label htmlFor="content" className="block text-sm font-medium text-gray-700 mb-2">
              Content *
            </label>
            <textarea
              id="content"
              name="content"
              value={formData.content}
              onChange={handleChange}
              rows={15}
              className={`w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-vertical ${errors.content ? 'border-red-500' : 'border-gray-300'
                }`}
              placeholder="Write your blog post content here..."
              disabled={saving}
            />
            {errors.content && (
              <p className="mt-1 text-sm text-red-600">{errors.content}</p>
            )}
            <p className="mt-1 text-sm text-gray-500">
              {formData.content.length} characters
            </p>
          </div>

          {/* Submit error */}
          {errors.submit && (
            <div className="bg-red-50 border border-red-200 rounded-md p-4">
              <p className="text-red-800">{errors.submit}</p>
            </div>
          )}

          {/* Action buttons */}
          <div className="flex flex-col sm:flex-row gap-3 pt-4">
            <button
              type="submit"
              disabled={saving}
              className="flex items-center justify-center px-6 py-3 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200"
            >
              {saving ? (
                <>
                  <LoadingSpinner size="sm" className="mr-2" />
                  Saving...
                </>
              ) : (
                'Save Changes'
              )}
            </button>
            <button
              type="button"
              onClick={handleCancel}
              disabled={saving}
              className="px-6 py-3 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200"
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditBlogPage;