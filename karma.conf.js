module.exports = function (config) {
  // junitReporter.outputDir is resolved relative to basePath ('target'
  // below), so this must NOT also include the "target/" prefix or the
  // report ends up written to target/target/junit.
  var junitOutputDir = process.env.CIRCLE_TEST_REPORTS || "junit"

  config.set({
    browsers: ['ChromeHeadless'],
    basePath: 'target',
    files: ['karma-test.js'],
    frameworks: ['cljs-test'],
    plugins: [
        'karma-cljs-test',
        'karma-chrome-launcher',
        'karma-junit-reporter'
    ],
    colors: true,
    logLevel: config.LOG_INFO,
    client: {
      args: ['shadow.test.karma.init']
    },

    // the default configuration
    junitReporter: {
      outputDir: junitOutputDir + '/karma', // results will be saved as outputDir/browserName.xml
      outputFile: undefined, // if included, results will be saved as outputDir/browserName/outputFile
      suite: '' // suite will become the package name attribute in xml testsuite element
    }
  })
}
