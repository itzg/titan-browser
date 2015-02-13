angular.module('titanBrowserApp', [
    'ui.bootstrap',
    'titanBrowserDirectives'
])
    .config(function($locationProvider) {
        $locationProvider.html5Mode(true);
    })

    .controller('TitanBrowserCtrl', function ($scope, $modal, $http, $q, $location, $rootScope) {

        $scope.edgesIn = [];
        $scope.edgesOut = [];
        $scope.focus = {};
        $scope.status = null;
        $scope.configurationOpen = false;

        var seedHost = "localhost";
        var primaryProperty;
        var actionDeferred;

        function setStatus(message, isError, showCancelAction) {
            $scope.status = {
                message: message,
                type: isError ? 'danger' : 'info',
                showCancelAction: showCancelAction
            }
        }

        function clearStatus() {
            $scope.status = null;
        }

        function pushEdges(resultEdges, scopeEdges) {
            if (resultEdges) {
                $.each(resultEdges, function (i, edge) {
                    var adjacentId = edge.adjacent.id;
                    var edgeSpec = {
                        adjacentId: adjacentId,
                        label: edge.label
                    };

                    // Put a placeholder
                    edgeSpec.primaryProperty = '...';
                    // ...then asynchronously go get the actual value
                    $http.get('/vertex/' + adjacentId + '/properties/' + primaryProperty, {
                        responseType: "text"
                    })
                        .success(function(result) {
                            if (result) {
                                edgeSpec.primaryProperty = result;
                            }
                            else {
                                edgeSpec.primaryProperty = '???';
                            }
                        });

                    scopeEdges.push(edgeSpec);
                });
            }
        }

        function changeLocation(searchParams) {
            console.debug("Changing location", searchParams);
            //if ($scope.settingLocation) {
            //    $scope.settingLocation = false;
            //}
            //else {
                $scope.settingLocation = true;
            //}
            angular.forEach(searchParams, function(val, key) {
                $location.search(key, val);
            });
        }

        function resetSession(newFocusId) {
            putSeedHost(seedHost)
                .done(function () {
                    getServerConfig();

                    focusOn(newFocusId);
                })
                .fail(function() {
                    $scope.startOver();
                });

        }

        function focusOn(vertexId) {
            console.debug("focusOn", vertexId);

            $scope.edgesIn = [];
            $scope.edgesOut = [];
            $scope.focus = {id:vertexId};
            $scope.multipleInFocus = false;

            changeLocation({i: vertexId});

            $http.get('/vertex/'+vertexId+'/surroundings')
                .success(function(result){
                    var val = result.properties[primaryProperty];
                    $scope.focus.primaryProperty = val;

                    $rootScope.subtitle = ' - ' + val + '[' + vertexId + ']';
                    // force a refresh of the History state
                    $location.replace();

                    delete result.properties._ID_;
                    $scope.focus.properties = result.properties;
                    pushEdges(result.edgesIn, $scope.edgesIn);
                    pushEdges(result.edgesOut, $scope.edgesOut);
                })
                .error(function() {
                    resetSession(vertexId);
                });
        };

        function handleRefocus(evt, vertexId) {
            console.debug("Handle refocus");
            $scope.handlingRefocus = true;
            focusOn(vertexId);
        }

        function handleLocationChangeStart(evt) {
            console.debug("handleLocationChangeStart");

            if (!$scope.handlingRefocus) {
                var currentSearchParams = $location.search();
                seedHost = currentSearchParams['s'] || seedHost;
                primaryProperty = currentSearchParams['p'] || primaryProperty;

                var newFocusId = currentSearchParams['i'];

                if (angular.isDefined(seedHost) && angular.isDefined(primaryProperty) && angular.isDefined(newFocusId)) {
                    focusOn(newFocusId);
                }
                else {
                    $scope.startOver();
                }
            }
            else {
                $scope.handlingRefocus = false;
                console.debug("Resetting handlingRefocus");
            }
        }

        /**
         *
         * @param newSeedHost
         * @returns {jQuery Deferred} the pending server operation
         */
        function putSeedHost(newSeedHost) {
            return $.ajax('configuration', {
                type: 'PUT',
                data: {
                    'titan.cassandra.seedHost': newSeedHost
                }
            })
        }

        function startSession(searchProperties) {
            console.debug("startSession", searchProperties);

            changeLocation({
                p: primaryProperty,
                s: seedHost
            });

            putSeedHost(seedHost)
                .done(function () {
                    getServerConfig();

                    $http.get('/search/vertex', {
                        params: searchProperties,
                        timeout: actionDeferred.promise
                    })
                        .success(function (result) {
                            if (angular.isArray(result)) {
                                if (result.length > 0) {
                                    var focusId = result[0]._ID_;

                                    clearStatus();
                                    $scope.handlingRefocus = true;
                                    focusOn(focusId);

                                    $scope.multipleInFocus = result.length > 1;
                                }
                                else {
                                    setStatus("No matches", true, false);
                                }
                            }
                        })
                        .error(function (data, status) {
                            setStatus(status === 0 ? "Search cancelled." :
                            "REST operation failed with " + status + ". " + data, true, false);
                        });
                })
                .fail(function (jqHDR, textStatus) {
                    setStatus("Failed to configure seed host: " +
                    jqHDR.responseJSON.error + " : " + jqHDR.responseJSON.message, true, false);
                    $scope.$apply();
                });
        }

        function getServerConfig() {

            $http.get('/configuration')
                .success(function(result){
                    $scope.serverConfig = result;
                });
        }

        $scope.cancelAction = function() {
            if (angular.isDefined(actionDeferred)) {
                actionDeferred.resolve("cancelled");
            }
        }

        $scope.startOver = function() {
            console.debug("startOver");

            var startingPointModal = $modal.open({
                templateUrl: 'bits/starting-point-modal.html',
                controller: 'StartingPointModalCtrl',
                resolve: {
                    primaryProperty: function() {return primaryProperty},
                    searchObject: function() {return $scope.focus.properties; },
                    seedHost: function() { return seedHost; }
                }
            });

            startingPointModal.result.then(function (result) {
                console.debug("Dialog gave us", result);
                setStatus("Searching...", false, true);
                $scope.edgesIn = [];
                $scope.edgesOut = [];
                $scope.focus = {};
                $scope.multipleInFocus = false;

                actionDeferred = $q.defer();

                seedHost = result.seedHost;
                primaryProperty = result.primaryProperty;

                $scope.handlingRefocus = true;
                startSession(result.searchProperties);
            });
        };

        $scope.$on('refocus', handleRefocus);

        $scope.$on("$locationChangeStart", handleLocationChangeStart);
    })

    .controller('StartingPointModalCtrl', function ($scope, primaryProperty, searchObject, seedHost) {

        function convertSearchObject(rawObj) {
            if (angular.isObject(rawObj)) {
                return $.map(rawObj, function(value,key) {
                    return {key:key, value:value};
                })
            }
            else {
                return [{key: '', value: ''}]
            }
        }

        $scope.seedHost = seedHost || 'localhost';
        $scope.primaryProperty = primaryProperty || '';
        $scope.propertySpecs = convertSearchObject(searchObject);
        $scope.normalize = 'lower';

        $scope.addAnother = function (isLast) {
            if (isLast) {
                $scope.propertySpecs.push({key: '', value: ''});
            }
        };

        $scope.handleInputFocus = function($event, $last) {
            if ($last) {
                if ($event.target.value != '') {
                    $scope.propertySpecs.push({key: '', value: ''});
                }
            }
        };

        $scope.removeProperty = function(index) {
            $scope.propertySpecs.splice(index, 1);
        }

        $scope.ok = function () {
            if ($scope.primaryProperty.length > 0) {

                var searchProperties = {};

                $.each($.grep($scope.propertySpecs, function (entry) {
                    return entry.key.length > 0;
                }), function (index, entry) {
                    var value = entry.value;
                    if ($scope.normalize == 'lower') {
                        value = angular.lowercase(value);
                    }
                    else if ($scope.normalize == 'upper') {
                        value = angular.uppercase(value);
                    }

                    searchProperties[entry.key] = value;
                });

                $scope.$close({
                    seedHost: $scope.seedHost,
                    primaryProperty: $scope.primaryProperty,
                    searchProperties: searchProperties
                })
            }
        };

        $scope.cancel = function () {
            $scope.$dismiss();
        }
    })
;