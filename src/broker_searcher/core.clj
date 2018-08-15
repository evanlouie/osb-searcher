(ns broker-searcher.core
  (:use clojure.pprint)
  (:require [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.core.async :as async]
            [clojure.core.match :as match]
            [clojure.data.json :as json])
  (:gen-class))

(defn- get-resource
  "Return a keyword indexed map of a fetched JSON resource"
  [^String endpoint]
  (let [host "http://127.0.0.1:8001"
        clean-endpoint (if (string/starts-with? endpoint "/") endpoint (str "/" endpoint))]
    (json/read-str (slurp (str host clean-endpoint)) :key-fn keyword)))

(defn- get-namespaces
  "Get a list of namespaces from the cluster"
  []
  (let [response-map (get-resource "api/v1/namespaces")
        namespaces (get response-map :items)]
    (map (fn [namespace] (get-in namespace [:metadata :name])) namespaces)))

(defn- get-service-catalog-resource
  "Return a list of resources from the service catalog endpoint"
  ([^String resource]
   (get-resource (str "apis/servicecatalog.k8s.io/v1beta1/" resource)))
  ([^String resource ^String nmspc]
   (get-resource (str "apis/servicecatalog.k8s.io/v1beta1/namespaces/" nmspc "/" resource))))

(def get-service-brokers
  "Returns a list of ServiceBrokers in the cluster; accepts a optional namespace argument."
  (partial get-service-catalog-resource "servicebrokers"))

(def get-service-classes
  "Returns a list of ServiceClasses in the cluster; accepts a optional namespace argument."
  (partial get-service-catalog-resource "serviceclasses"))

(def get-service-plans
  "Returns a list of ServicePlans in the cluster; accepts a optional namespace argument."
  (partial get-service-catalog-resource "serviceplans"))

(def get-service-instances
  "Returns a list of ServiceInstances in the cluster; accepts a optional namespace argument."
  (partial get-service-catalog-resource "serviceinstances"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Main
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def cli-options
  ;; An option with a required argument
  [["-r" "--resource RESOURCE" "Kubernetes API resource type"
    :default "servicebrokers"
    :parse-fn (fn [n] (str n))
    :validate (let [supported ["servicebrokers"
                               "serviceclasses"
                               "serviceplans"
                               "serviceinstances"]]
                [(fn [r] (some (partial = r) supported))
                 (str "Must be one of the supported CRD's:" supported)])]
   ["-p" "--port PORT" "Port number"
    :default 80
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ;; A non-idempotent option (:default is applied first)
   ["-v" nil "Verbosity level"
    :id :verbosity
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ;; A boolean option defaulting to nil
   ["-h" "--help"]])

(defn -main
  "I do stuff"
  [& args]
  (do
    (pprint (parse-opts args cli-options))
    (comment (pprint (json/write-str (->> (get-namespaces)
                                          (pmap get-service-plans)))))
    (shutdown-agents)))
