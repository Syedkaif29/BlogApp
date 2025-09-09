import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { blogAPI } from '../services/api';
import Pagination from '../components/Pagination';
import LoadingSpinner from '../components/LoadingSpinner';
import SearchInput from '../components/SearchInput';

const HomePage = () => {
  const { user } = useAuth();
  const [blogs, setBlogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState('date');
  const [selectedTags, setSelectedTags] = useState([]);
  const [isSearching, setIsSearching] = useState(false);

  const fetchBlogs = async (page = 0, isSearch = false) => {
    try {
      if (isSearch) {
        setIsSearching(true);
      } else {
        setLoading(true);
      }
      setError(null);
      
      const tagsParam = selectedTags.length > 0 ? selectedTags.join(',') : '';
      const response = await blogAPI.getBlogs(page, 10, sortBy, searchTerm, tagsParam);
      
      setBlogs(response.content || []);
      setTotalPages(response.totalPages || 0);
      setTotalElements(response.totalElements || 0);
      setCurrentPage(page);
    } catch (err) {
      setError(err.message || 'Failed to load blogs');
    } finally {
      setLoading(false);
      setIsSearching(false);
    }
  };

  // Initial load
  useEffect(() => {
    fetchBlogs(0);
  }, []);

  // Search effect - no debouncing needed as SearchInput handles it
  useEffect(() => {
    if (searchTerm !== '') {
      fetchBlogs(0, true); // Mark as search to avoid full loading state
    } else {
      fetchBlogs(0); // Regular fetch for empty search
    }
  }, [searchTerm]);

  // Immediate effect for sort and tags
  useEffect(() => {
    fetchBlogs(0);
  }, [sortBy, selectedTags]);



  const handlePageChange = (page) => {
    fetchBlogs(page);
  };

  const handleSearch = useCallback((term) => {
    setSearchTerm(term);
  }, []);

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const truncateContent = (content, maxLength = 200) => {
    if (!content || typeof content !== 'string') return '';
    if (content.length <= maxLength) return content;
    return content.substring(0, maxLength) + '...';
  };

  if (loading && !isSearching) {
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
          <button
            onClick={() => fetchBlogs(currentPage)}
            className="mt-2 text-red-600 hover:text-red-800 underline"
          >
            Try again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-800">Welcome to BlogApp</h1>
          <p className="text-gray-600 mt-2">
            Discover and share amazing blog posts from our community.
          </p>
          {totalElements > 0 && (
            <p className="text-sm text-gray-500 mt-1">
              {totalElements} blog{totalElements !== 1 ? 's' : ''} found
            </p>
          )}
        </div>
        {user && (
          <Link
            to="/create"
            className="bg-blue-600 text-white px-6 py-3 rounded-md hover:bg-blue-700 transition-colors duration-200 font-medium"
          >
            Create New Post
          </Link>
        )}
      </div>

      {/* Search and Filter */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
        {/* Search Bar */}
        <SearchInput 
          onSearch={handleSearch} 
          isSearching={isSearching}
          placeholder="Search blogs..."
        />

        {/* Sort Controls */}
        <div className="flex items-center space-x-2">
          <label className="text-sm font-medium text-gray-700">Sort by:</label>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="border border-gray-300 rounded-md px-3 py-1 text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          >
            <option value="date">Latest</option>
            <option value="popularity">Most Popular</option>
            <option value="title">Alphabetical</option>
          </select>
        </div>
      </div>

      {blogs.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-500 text-lg">No blog posts found</p>
          <p className="text-gray-400 mt-2">Be the first to create a blog post!</p>
          {user && (
            <Link
              to="/create"
              className="inline-block mt-4 bg-blue-600 text-white px-6 py-3 rounded-md hover:bg-blue-700 transition-colors duration-200 font-medium"
            >
              Create Your First Post
            </Link>
          )}
        </div>
      ) : (
        <>
          <div className="space-y-6">
            {blogs.map((blog) => (
              <article
                key={blog.id}
                className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow duration-200"
              >
                <div className="flex flex-col sm:flex-row sm:justify-between sm:items-start mb-4">
                  <Link
                    to={`/blog/${blog.id}`}
                    className="text-xl font-semibold text-gray-800 hover:text-blue-600 transition-colors duration-200"
                  >
                    {blog.title}
                  </Link>
                  <div className="text-sm text-gray-500 mt-2 sm:mt-0 sm:ml-4 flex-shrink-0">
                    <p>By {blog.author?.firstName} {blog.author?.lastName}</p>
                    <p>{formatDate(blog.createdAt)}</p>
                  </div>
                </div>
                <p className="text-gray-600 leading-relaxed">
                  {blog.contentPreview || truncateContent(blog.content)}
                </p>
                
                {/* Tags */}
                {blog.tags && blog.tags.length > 0 && (
                  <div className="mt-3 flex flex-wrap gap-2">
                    {blog.tags.map((tag, index) => (
                      <span
                        key={index}
                        className="px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded-full"
                      >
                        {tag}
                      </span>
                    ))}
                  </div>
                )}
                
                <div className="mt-4 flex justify-between items-center">
                  <Link
                    to={`/blog/${blog.id}`}
                    className="text-blue-600 hover:text-blue-800 font-medium"
                  >
                    Read more →
                  </Link>
                  {blog.viewCount > 0 && (
                    <span className="text-sm text-gray-500">
                      {blog.viewCount} view{blog.viewCount !== 1 ? 's' : ''}
                    </span>
                  )}
                </div>
              </article>
            ))}
          </div>

          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={handlePageChange}
          />
        </>
      )}
    </div>
  );
};

export default HomePage;