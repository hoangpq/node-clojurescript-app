# Standalone Usage

```bash
$ npm install
$ npm install -g nodemon
$ lein figwheel
# (In another window)
$ nodemon target\js\compiled\node_clj.js
```

# Production Builds
1. `lein cljsbuild once prod`
2. `node server.js ...`

# Code Style
1. `lein cljfmt check`
2. `lein cljfmt fix` 
