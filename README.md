# Catlantis

A simple [React-Native](https://facebook.github.io/react-native/) IOS app (about Cats!) written in [Clojurescript](https://github.com/clojure/clojurescript) using [re-natal](https://github.com/drapanjanas/re-natal) template.

Notable libraries used:
* [react-native-navigation](https://github.com/wix/react-native-navigation)
* [re-frame](https://github.com/Day8/re-frame)
* [react-native-extended-stylesheet](https://github.com/vitalets/react-native-extended-stylesheet)

APIs used:
* [The Cat API](http://thecatapi.com/)
* [Cat Facts API](http://catfacts-api.appspot.com/)

![gif demo](https://cloud.githubusercontent.com/assets/3857155/15516250/4f593634-21f2-11e6-84f2-c733b77cca32.gif)

## How to run
```
$ npm i
$ lein prod-build
```
Then run in iOS from xcode or `react-native run-ios`

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