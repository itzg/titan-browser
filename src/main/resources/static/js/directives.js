angular.module('titanBrowserDirectives', [])
    .directive('titanBrowserEdgeListing', function ($rootScope) {
        return {
            templateUrl: 'bits/edge-listing.html',
            restrict: 'A',
            scope: {
                edges: '=edges',
                edgeClass: '@edgeClass'
            },
            controller: function($scope) {
                $scope.focusOn = function(vertexId) {
                    $rootScope.$broadcast('refocus', vertexId);
                }
            }
        };
    })
;