(function() {
    'use strict';

    angular
        .module('pru1App')
        .controller('CiudadDetailController', CiudadDetailController);

    CiudadDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Ciudad', 'Pais'];

    function CiudadDetailController($scope, $rootScope, $stateParams, previousState, entity, Ciudad, Pais) {
        var vm = this;

        vm.ciudad = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('pru1App:ciudadUpdate', function(event, result) {
            vm.ciudad = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
