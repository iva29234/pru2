(function() {
    'use strict';
    angular
        .module('pru1App')
        .factory('Ciudad', Ciudad);

    Ciudad.$inject = ['$resource', 'DateUtils'];

    function Ciudad ($resource, DateUtils) {
        var resourceUrl =  'api/ciudads/:id';

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
