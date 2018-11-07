#! /usr/bin/env node

if (!process.env.TRAVIS_REPO_SLUG) {
  process.exit(0);
}

const https = require('https');

const postData = `{\"body\": \"**Snapshot Tests**\nReport: https://happo.io/a/27/report/${process.env.GIT_SHA}-android26\nDiff: https://happo.io/a/27/compare/master-android26/${process.env.GIT_SHA}-android26\n\"}`

const options = {
  hostname: 'api.github.com',
  path: `/repos/${process.env.TRAVIS_REPO_SLUG}/issues/${process.env.TRAVIS_PULL_REQUEST}/comments`,
  port: 443,
  method: 'POST',
  headers: {
    Authorization: `token ${process.env.GITHUB_ACCESS_TOKEN}`,
    'Content-Length': postData.length,
    'User-Agent': 'Travis/1.6.8 (Mac OS X 10.9.2 like Darwin; Ruby 2.1.1; RubyGems 2.0.14) Faraday/0.8.9 Typhoeus/0.6.7.'
 }
};

const req = https.request(options, res => {
  console.log('statusCode:', res.statusCode);
  console.log('headers:', res.headers);

  res.on('data', d => {
    process.stdout.write(d);
  });
  res.on('error', e => {
    process.stderr.write(e);
  })
})
req.write(postData);
req.end();