var Settings = (function() {

	return {

		init: function() {
			$('#update_template_inp').click(function() {
				var self = $(this);
				setTimeout(function () {
                    $('#update_template').val(
                        self.is(":checked") ? "true" : "false"
                    );
                }, 100);

			});
            $('#save_requests_inp').click(function() {
                var self = $(this);
                setTimeout(function () {
                    $('#save_requests').val(
                        self.is(":checked") ? "true" : "false"
                    );
                }, 100);

            });

            $('#settingsForm').submit(function(event) {
                Settings.save();
            	event.preventDefault();
			});
		},

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