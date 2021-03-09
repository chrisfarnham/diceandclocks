

deps:
    lein deps

css:
    postcss -o resources/public/css/output.css resources/public/css/styles.css

dev:
    cp resources/public/dev-index.html resources/public/index.html
    just css

prod:
    cp resources/public/prod-index.html resources/public/index.html
    just css

release:
    just prod
    lein release

deploy:
    just release
    firebase deploy