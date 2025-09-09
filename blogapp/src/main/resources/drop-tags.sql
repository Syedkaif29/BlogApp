-- Script to remove tag-related tables and constraints
-- Run this manually in your database to clean up existing tag data

-- Drop the blog_tags junction table first (due to foreign key constraints)
DROP TABLE IF EXISTS blog_tags CASCADE;

-- Drop the tags table
DROP TABLE IF EXISTS tags CASCADE;

-- Drop tag-related indexes if they exist
DROP INDEX IF EXISTS idx_tags_name;