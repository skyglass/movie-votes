module.exports = {
  basePath: '/bets-ui',
  assetPrefix: '/bets-ui',
  webpack: (config) => {
    config.watchOptions.poll = 300;
    return config;
  },
};