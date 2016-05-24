(ns catlantis.api-routes)

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