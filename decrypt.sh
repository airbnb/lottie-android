if [ "$TRAVIS_REPO_SLUG" != "airbnb/lottie-android" ]; then
  echo "Skipping decrypt because api keys are not available from forks."
  exit 0
fi
openssl aes-256-cbc -K $encrypted_7f6a0d70974a_key -iv $encrypted_7f6a0d70974a_iv -in secrets.tar.enc -out secrets.tar -d
tar xvf secrets.tar