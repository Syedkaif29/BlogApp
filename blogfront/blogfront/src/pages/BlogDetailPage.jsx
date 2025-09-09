import { useState, useEffect, useContext } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { blogAPI } from '../services/api';
import { AuthContext } from '../contexts/AuthContext';
import { useToast } from '../contexts/ToastContext';
import LoadingSpinner from '../components/LoadingSpinner';
import ConfirmModal from '../components/ConfirmModal';
import CommentSection from '../components/CommentSection';

const BlogDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  const { showSuccess, showError } = useToast();
  const [blog, setBlog] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  useEffect(() => {
    const fetchBlog = async () => {
      try {
        setLoading(true);
        setError(null);
        const response = await blogAPI.getBlog(id);
        setBlog(response);
      } catch (err) {
        setError(err.message || 'Failed to load blog post');
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchBlog();
    }
  }, [id]);

  const handleDeleteClick = () => {
    setShowDeleteModal(true);
  };

  const handleDeleteConfirm = async () => {
    try {
      setDeleting(true);
      setShowDeleteModal(false);
      await blogAPI.deleteBlog(id);
      showSuccess('Blog post deleted successfully!');
      navigate('/', { replace: true });
    } catch (err) {
      showError(err.message || 'Failed to delete blog post');
      setDeleting(false);
    }
  };

  const handleDeleteCancel = () => {
    setShowDeleteModal(false);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const isAuthor = user && blog && user.id === blog.author?.id;

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto">
        <LoadingSpinner size="lg" className="py-12" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-4xl mx-auto">
        <div className="bg-red-50 border border-red-200 rounded-md p-4">
          <p className="text-red-800">{error}</p>
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

  if (!blog) {
    return (
      <div className="max-w-4xl mx-auto">
        <div className="text-center py-12">
          <p className="text-gray-500">Blog post not found</p>
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

  return (
    <div className="max-w-4xl mx-auto">
      {/* Navigation */}
      <div className="mb-6">
        <Link
          to="/"
          className="text-blue-600 hover:text-blue-800 font-medium"
        >
          ← Back to blogs
        </Link>
      </div>

      {/* Blog content */}
      <article className="bg-white rounded-lg shadow-md p-6 lg:p-8">
        {/* Header */}
        <header className="mb-8">
          <h1 className="text-3xl lg:text-4xl font-bold text-gray-800 mb-4 leading-tight">
            {blog.title}
          </h1>
          <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center text-gray-600">
            <div className="flex items-center space-x-4">
              <span className="font-medium">By {blog.author?.firstName} {blog.author?.lastName}</span>
              <span>•</span>
              <time dateTime={blog.createdAt}>
                {formatDate(blog.createdAt)}
              </time>
            </div>
            {blog.updatedAt && blog.updatedAt !== blog.createdAt && (
              <div className="text-sm text-gray-500 mt-2 sm:mt-0">
                Updated: {formatDate(blog.updatedAt)}
              </div>
            )}
          </div>
        </header>

        {/* Tags */}
        {blog.tags && blog.tags.length > 0 && (
          <div className="mb-6">
            <div className="flex flex-wrap gap-2">
              {blog.tags.map((tag, index) => (
                <span
                  key={index}
                  className="px-3 py-1 text-sm bg-blue-100 text-blue-800 rounded-full"
                >
                  {tag}
                </span>
              ))}
            </div>
          </div>
        )}

        {/* Content */}
        <div className="prose prose-lg max-w-none">
          <div className="text-gray-700 leading-relaxed whitespace-pre-wrap">
            {blog.content}
          </div>
        </div>

        {/* View count */}
        {blog.viewCount > 0 && (
          <div className="mt-6 text-sm text-gray-500">
            {blog.viewCount} view{blog.viewCount !== 1 ? 's' : ''}
          </div>
        )}

        {/* Author actions */}
        {isAuthor && (
          <div className="mt-8 pt-6 border-t border-gray-200">
            <div className="flex flex-col sm:flex-row gap-3">
              <Link
                to={`/edit/${blog.id}`}
                className="inline-flex items-center justify-center px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors duration-200"
              >
                Edit Post
              </Link>
              <button
                onClick={handleDeleteClick}
                disabled={deleting}
                className="inline-flex items-center justify-center px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200"
              >
                {deleting ? (
                  <>
                    <LoadingSpinner size="sm" className="mr-2" />
                    Deleting...
                  </>
                ) : (
                  'Delete Post'
                )}
              </button>
            </div>
          </div>
        )}
      </article>

      {/* Comments Section */}
      <CommentSection blogId={blog.id} />

      {/* Delete Confirmation Modal */}
      <ConfirmModal
        isOpen={showDeleteModal}
        onClose={handleDeleteCancel}
        onConfirm={handleDeleteConfirm}
        title="Delete Blog Post"
        message={`Are you sure you want to delete "${blog?.title}"? This action cannot be undone.`}
        confirmText="Delete"
        cancelText="Cancel"
        type="danger"
      />
    </div>
  );
};

export default BlogDetailPage;