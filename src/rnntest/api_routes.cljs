(ns rnntest.api-routes
  (:require [bidi.bidi :as b]))

(def api-routes [""
                 {"http://thecatapi.com/api/"
                  {"categories/list"   :categories
                   "images/"           {"get"           :images
                                        "vote"          :vote
                                        "getvotes"      :votes
                                        "favourite"     :favorite
                                        "getfavourites" :favorites
                                        "report"        :report}
                   "stats/getoverview" :overview}
                  "http://catfacts-api.appspot.com/api/facts"
                  :facts}])

(comment
  (b/path-for api-routes :categories)
  (b/path-for api-routes :images)
  (b/path-for api-routes :facts))