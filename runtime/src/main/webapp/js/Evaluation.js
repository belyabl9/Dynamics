var Evaluation = (function() {
	
	return {

		evaluate: function() {
			var self = this;
			var password = $('#password').val();
	        	$.ajax({
	        		url: '/ajax/eval?password=' + password,
	                beforeSend: function() {
	                    $('#pleaseWaitDialog').modal();
	                },
	                success: function(data) {
	                    $('#results').val(data.evalResults);
	                    $('#configuration').val(data.configuration);
	                    $('#pleaseWaitDialog').modal('hide');
	                },
	                error: function() {
	                    $('#pleaseWaitDialog').modal('hide');
	                }
	        	});
		}
	
	};
})();