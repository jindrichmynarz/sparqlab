(ns sparqlab.layout
  (:require [sparqlab.i18n :as i18n]
            [selmer.parser :as parser]
            [selmer.filters :as filters]
            [markdown.core :refer [md-to-html-string]]
            [ring.util.http-response :refer [content-type ok]]
            [clojure.java.io :as io]))

(declare ^:dynamic *app-context*)

(parser/set-resource-path! (clojure.java.io/resource "templates"))

; Selmer template filters
(filters/add-filter! :dec dec)

(filters/add-filter! :markdown (fn [content] [:safe (md-to-html-string content)]))

(filters/add-filter! :inline-markdown
                     (fn [content]
                       (let [wrapped (md-to-html-string content)]
                         [:safe (subs wrapped 3 (- (count wrapped) 4))])))

(defn render
  "renders the HTML template located relative to resources/templates"
  [template & [params]]
  (content-type
    (ok
      (parser/render-file
        template
        (assoc params
          :page template
          :servlet-context *app-context*)))
    "text/html; charset=utf-8"))

(defn error-page
  "error-details should be a map containing the following keys:
   :status - error status
   :title - error title (optional)
   :message - detailed error message (optional)

   returns a response map with the error page as the body
   and the status specified by the status key"
  [{tr :tempura/tr
    :as request}
   error-details]
  {:status  (:status error-details)
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    (parser/render-file "error.html" (merge (i18n/base-locale request)
                                                    error-details
                                                    {:label (tr [:error/title])
                                                     :servlet-context *app-context*}))})

(defn not-found
  "Page not found"
  [{tr :tempura/tr
    :as request}]
  (error-page request
              {:status 404
               :title (tr [:not-found/title])}))
