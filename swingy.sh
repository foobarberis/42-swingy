#!/usr/bin/env sh

case "$1" in
  clean)
    exec mise exec -- mvn clean
    ;;
  build)
    exec mise exec -- mvn clean package
    ;;
  test)
    exec mise exec -- mvn test
    ;;
  gui)
    exec mise exec -- java -jar target/swingy.jar gui
    ;;
  cli)
    exec mise exec -- java -jar target/swingy.jar console
    ;;
  *)
    printf 'Usage: %s {clean|build|test|gui|cli}\n' "$0" >&2
    exit 1
    ;;
esac
