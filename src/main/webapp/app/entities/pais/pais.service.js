(function() {
    'use strict';
    angular
        .module('pru1App')
        .factory('Pais', Pais);

    Pais.$inject = ['$resource', 'DateUtils'];

    function Pais ($resource, DateUtils) {
        var resourceUrl =  'api/pais/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.createdDate = DateUtils.convertDateTimeFromServer(data.createdDate);
                        data.modifiedDate = DateUtils.convertDateTimeFromServer(data.modifiedDate);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
