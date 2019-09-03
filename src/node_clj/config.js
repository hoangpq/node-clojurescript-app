goog.provide("node_clj.config");

const fs = require('fs');
const path = require('path');

const configPath = path.join(__dirname, 'resources/config/config.json');

node_clj.config.loadConfig = function () {
    try {
        return JSON.parse(fs.readFileSync(configPath).toString());
    } catch (e) {
        throw e;
    }
};
