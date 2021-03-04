

deps:
    lein deps

dev:
    cp resources/public/dev-index.html resources/public/index.html
    postcss -o resources/public/css/output.css resources/public/css/styles.css

prod:
    cp resources/public/prod-index.html resources/public/index.html
    postcss -o resources/public/css/output.css resources/public/css/styles.css

release:
    just prod
    lein release

deploy:
    just release
    firebase deploy