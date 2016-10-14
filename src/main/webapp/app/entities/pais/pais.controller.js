(function() {
    'use strict';

    angular
        .module('pru1App')
        .controller('PaisController', PaisController);

    PaisController.$inject = ['$scope', '$state', 'Pais', 'ParseLinks', 'AlertService', 'pagingParams', 'paginationConstants'];

    function PaisController ($scope, $state, Pais, ParseLinks, AlertService, pagingParams, paginationConstants) {
        var vm = this;
        
        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;

        loadAll();

        function loadAll () {
            Pais.query({
                page: pagingParams.page - 1,
                size: vm.itemsPerPage,
                sort: sort()
            }, onSuccess, onError);
            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'id') {
                    result.push('id');
                }
                return result;
            }
            function onSuccess(data, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.pais = data;
                vm.page = pagingParams.page;

                $(".tabla-pais tfoot th").each(function(){
                    var title=$(this).text();
                    $(this).html("<input type='text' placeholder='Buscar "+title +"'/>");
                })




                var tabla = $(".tabla-pais").DataTable({
                    "paging":false,
                    "ordering":true,
                    "info": false
                });
                
                tabla.columns().every(function(){
                	var that =this;
                	
                	$("input",this.footer()).on("keyup change", function(){
                		if (that.search()!==this.value){
                			that.search(this.value).draw();
                		}
                	})
                })
                
                
                
            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        function loadPage (page) {
            vm.page = page;
            vm.transition();
        }

        function transition () {
            $state.transitionTo($state.$current, {
                page: vm.page,
                sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
                search: vm.currentSearch
            });
        }
    }
})();
