var Settings = (function() {

	return {
		
		save: function() {
			var params = $('#settingsForm').serialize();

        	$.ajax({
        		url: '/ajax/updateSettings',
        		data: params,
        		beforeSend: function() {
        			$('#pleaseWaitDialog').modal();
        		},
        		success: function(data) {
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