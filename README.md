# Catlantis

A simple [React-Native](https://facebook.github.io/react-native/) IOS app (about Cats!) written in [Clojurescript](https://github.com/clojure/clojurescript) using [re-natal](https://github.com/drapanjanas/re-natal) template.

## How to run
```
$ npm i
$ lein prod-build
```
Then run in iOS from xcode or android by `react-native run-android`

## How to develop
Catlantis is based on re-natal 0.2.34+.
```
$ re-natal use-figwheel
$ lein figwheel ios
```
or nREPL
```
$ lein repl
user=> (start-ios-fig)
```
and finally
```
$ react-native run-ios
```

Please, refer to [re-natal documentation](https://github.com/drapanjanas/re-natal/blob/master/README.md) for more information.