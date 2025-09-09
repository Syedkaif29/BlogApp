import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8082';
const API_URL = `${API_BASE_URL}/api`;

// Create axios instance with base configuration
const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Handle unauthorized access
      console.log('401 Unauthorized - clearing auth data');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      // Only redirect if not already on login/register page
      if (!window.location.pathname.includes('/login') && !window.location.pathname.includes('/register')) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// Auth API functions
export const authAPI = {
  login: async (credentials) => {
    try {
      const response = await api.post('/auth/login', credentials);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Login failed' };
    }
  },
  register: async (userData) => {
    try {
      const response = await api.post('/auth/register', userData);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Registration failed' };
    }
  }
};

// Blog API functions
export const blogAPI = {
  getBlogs: async (page = 0, size = 10, sortBy = 'date', search = '') => {
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
        sortBy
      });
      
      if (search) params.append('search', search);
      
      const response = await api.get(`/blogs?${params.toString()}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Failed to fetch blogs' };
    }
  },
  getBlog: async (id) => {
    try {
      const response = await api.get(`/blogs/${id}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Failed to fetch blog' };
    }
  },
  createBlog: async (blogData) => {
    try {
      const response = await api.post('/blogs', blogData);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Failed to create blog' };
    }
  },
  updateBlog: async (id, blogData) => {
    try {
      const response = await api.put(`/blogs/${id}`, blogData);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Failed to update blog' };
    }
  },
  deleteBlog: async (id) => {
    try {
      await api.delete(`/blogs/${id}`);
      return { message: 'Blog deleted successfully' };
    } catch (error) {
      throw error.response?.data || { message: 'Failed to delete blog' };
    }
  }
};

// Tag API functions removed

// Comment API functions
export const commentAPI = {
  getComments: async (blogId, page = 0, size = 10) => {
    try {
      const response = await api.get(`/blogs/${blogId}/comments?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Failed to fetch comments' };
    }
  },
  createComment: async (blogId, commentData) => {
    try {
      const response = await api.post(`/blogs/${blogId}/comments`, commentData);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Failed to create comment' };
    }
  },
  updateComment: async (commentId, commentData) => {
    try {
      const response = await api.put(`/comments/${commentId}`, commentData);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Failed to update comment' };
    }
  },
  deleteComment: async (commentId) => {
    try {
      await api.delete(`/comments/${commentId}`);
      return { message: 'Comment deleted successfully' };
    } catch (error) {
      throw error.response?.data || { message: 'Failed to delete comment' };
    }
  },
  getUserComments: async (userId) => {
    try {
      const response = await api.get(`/users/${userId}/comments`);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Failed to fetch user comments' };
    }
  }
};

// User API functions
export const userAPI = {
  getProfile: async () => {
    try {
      const response = await api.get('/users/profile');
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Failed to fetch profile' };
    }
  },
  getUserProfile: async (userId) => {
    try {
      const response = await api.get(`/users/${userId}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Failed to fetch user profile' };
    }
  },
  updateProfile: async (profileData) => {
    try {
      const response = await api.put('/users/profile', profileData);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Failed to update profile' };
    }
  }
};

// Image API functions
export const imageAPI = {
  uploadImage: async (blogId, file) => {
    try {
      const formData = new FormData();
      formData.append('file', file);
      
      const response = await api.post(`/blogs/${blogId}/images`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Failed to upload image' };
    }
  },
  deleteImage: async (imageId) => {
    try {
      await api.delete(`/images/${imageId}`);
      return { message: 'Image deleted successfully' };
    } catch (error) {
      throw error.response?.data || { message: 'Failed to delete image' };
    }
  },
  getImageUrl: (filename) => {
    return `${API_URL}/images/${filename}`;
  }
};

export default api;