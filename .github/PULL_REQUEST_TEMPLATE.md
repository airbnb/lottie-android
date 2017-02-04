Before opening a pull request:

* If you added a new feature or fixed a bug, add a json file to assets/Tests and add a test 
for it in `LottieTest#testAll`.
* Run screenshot tests by running `./gradlew --daemon recordMode screenshotTests` on a Nexus 
5 5.1.0 emulator and commit all screenshots that have changed.
* Assign gpeal as a reviewer.