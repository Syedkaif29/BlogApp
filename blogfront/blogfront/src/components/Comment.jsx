import React, { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { commentAPI } from '../services/api';
import { useToast } from '../contexts/ToastContext';

const Comment = ({ comment, onUpdate, onDelete }) => {
  const { user } = useAuth();
  const { showToast } = useToast();
  const [isEditing, setIsEditing] = useState(false);
  const [editContent, setEditContent] = useState(comment.content);
  const [isLoading, setIsLoading] = useState(false);

  const isAuthor = user && user.id === comment.authorId;

  const handleEdit = async () => {
    if (!editContent.trim()) {
      showToast('Comment cannot be empty', 'error');
      return;
    }

    setIsLoading(true);
    try {
      const updatedComment = await commentAPI.updateComment(comment.id, {
        content: editContent.trim()
      });
      onUpdate(updatedComment);
      setIsEditing(false);
      showToast('Comment updated successfully', 'success');
    } catch (error) {
      showToast(error.message || 'Failed to update comment', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to delete this comment?')) {
      return;
    }

    setIsLoading(true);
    try {
      await commentAPI.deleteComment(comment.id);
      onDelete(comment.id);
      showToast('Comment deleted successfully', 'success');
    } catch (error) {
      showToast(error.message || 'Failed to delete comment', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleCancel = () => {
    setEditContent(comment.content);
    setIsEditing(false);
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className="bg-white border border-gray-200 rounded-lg p-4 mb-4">
      <div className="flex justify-between items-start mb-2">
        <div className="flex items-center space-x-2">
          <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center text-white text-sm font-medium">
            {comment.authorName.charAt(0).toUpperCase()}
          </div>
          <div>
            <p className="font-medium text-gray-900">{comment.authorName}</p>
            <p className="text-sm text-gray-500">
              {formatDate(comment.createdAt)}
              {comment.isEdited && (
                <span className="ml-2 text-xs text-gray-400">(edited)</span>
              )}
            </p>
          </div>
        </div>
        
        {isAuthor && (
          <div className="flex space-x-2">
            <button
              onClick={() => setIsEditing(!isEditing)}
              disabled={isLoading}
              className="text-blue-600 hover:text-blue-800 text-sm font-medium disabled:opacity-50"
            >
              {isEditing ? 'Cancel' : 'Edit'}
            </button>
            <button
              onClick={handleDelete}
              disabled={isLoading}
              className="text-red-600 hover:text-red-800 text-sm font-medium disabled:opacity-50"
            >
              Delete
            </button>
          </div>
        )}
      </div>

      {isEditing ? (
        <div className="space-y-3">
          <textarea
            value={editContent}
            onChange={(e) => setEditContent(e.target.value)}
            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 resize-none"
            rows="3"
            placeholder="Write your comment..."
          />
          <div className="flex space-x-2">
            <button
              onClick={handleEdit}
              disabled={isLoading || !editContent.trim()}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? 'Saving...' : 'Save'}
            </button>
            <button
              onClick={handleCancel}
              disabled={isLoading}
              className="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400 disabled:opacity-50"
            >
              Cancel
            </button>
          </div>
        </div>
      ) : (
        <div className="text-gray-800 whitespace-pre-wrap">{comment.content}</div>
      )}
    </div>
  );
};

export default Comment;