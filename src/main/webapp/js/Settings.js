var Settings = (function() {

	return {
		
		save: function() {
			var params = $('#settingsForm').serialize();
			console.log($('#settingsForm'));
			console.log('params');
			console.log(params);
			
        	$.ajax({
        		url: '/ajax/updateSettings',
        		data: params,
        		beforeSend: function() {
        			$('#pleaseWaitDialog').modal();
        		},
        		success: function(data) {
        			console.log(data);
        			$('#pleaseWaitDialog').modal('hide');
        			if (data && data.error) {
                		console.error("Can not update settings");
                	}
                },
                error: function() {
                	$('#pleaseWaitDialog').modal('hide');
                	console.error("Can not update settings");
                }
        	});
		}

	};
})();