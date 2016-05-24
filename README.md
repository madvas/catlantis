# Catlantis

A simple [React-Native](https://facebook.github.io/react-native/) IOS app (about Cats!) written in [Clojurescript](https://github.com/clojure/clojurescript) using [re-natal](https://github.com/drapanjanas/re-natal) template.

Notable libraries used:
* [react-native-navigation](https://github.com/wix/react-native-navigation)
* [re-frame](https://github.com/Day8/re-frame)
* [react-native-extended-stylesheet](https://github.com/vitalets/react-native-extended-stylesheet)

APIs used:
* [The Cat API](http://thecatapi.com/)
* [Cat Facts API](http://catfacts-api.appspot.com/)

![gif demo](https://cloud.githubusercontent.com/assets/3857155/15515694/bb3f2910-21ef-11e6-88f7-fb2177c4161b.gif)

![](https://cloud.githubusercontent.com/assets/3857155/15515991/fd183ff6-21f0-11e6-931f-57de11ddd3b4.png)
![](https://cloud.githubusercontent.com/assets/3857155/15515990/fd1848e8-21f0-11e6-9b53-f62ade514193.png)
![](https://cloud.githubusercontent.com/assets/3857155/15515988/fd1759c4-21f0-11e6-9edf-c9bb55630ad2.png)
![](https://cloud.githubusercontent.com/assets/3857155/15515989/fd18084c-21f0-11e6-8833-a2488c298306.png)

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