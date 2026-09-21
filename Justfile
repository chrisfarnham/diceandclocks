

deps:
    lein deps

css:
    npx postcss -o resources/public/css/output.css resources/public/css/styles.css

dev:
    just css

prod:
    just css

release:
    just prod
    lein release

deploy:
    just release
    firebase deploy

ci:
    lein ci