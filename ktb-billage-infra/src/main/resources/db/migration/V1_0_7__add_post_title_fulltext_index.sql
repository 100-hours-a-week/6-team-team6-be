ALTER TABLE `post`
  ADD FULLTEXT INDEX `ftx_post_title` (`title`) WITH PARSER ngram;
