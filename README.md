# PEM Project: Mensa-App


If you wish to add to this git repository, please adhere to 
https://chris.beams.io/posts/git-commit/
for commit message styles.

In particular, adhere to these rules in your commits:

1. Separate subject from body with a blank line
2. Limit the subject line to 50 characters
3. Capitalize the subject line
4. Do not end the subject line with a period
5. Use the imperative mood in the subject line
6. Wrap the body at 72 characters
7. Use the body to explain what and why vs. how

[Keep your commits atomic.](https://www.freshconsulting.com/atomic-commits/)
___________________________________________________________________________________________________________________________________
Feature branch workflow in git:

# Make a feature branch:

*  `git pull --ff-only`

*  `git checkout <your-branch-name>`

# Update your feature branch:

*  `git checkout master` (from your feature branch)

*  `git pull --ff-only` (update your local master branch)

*  `git checkout <your-branch-name>` (switch to your feature branch)

*  `git rebase master` (update your feature branch with the master branch)

# Merge your feature branch into master branch:

*  `git checkout master` (from your feature branch)

*  `git pull--ff-only` (update your local master branch)

*  `git merge <your-branch-name>`

*  `git push`

# HINT:
  If your feature branch is completely merged into the master, deleat this branch and create a new one.
  Don't rebase the already merged feature branch with the master. Then you'll definitely get merge conflicts.
  For deleting a (merged) feature branch:

*  `git branch -d <your-branch-name>`

  After that, just make a new feature branch from master and start again with your implementation in the workflow (see first steps...)

*  `git checkout <your-new-branch-name> `


