var Users = (function() {

	var ADMIN_USER_TYPE = 0;
	var REGULAR_USER_TYPE = 1;
	
	var usersTable;
	var usersTableId = 'usersTable';
	
	return {

		initUsersList: function() {
            usersTable = $('#' + usersTableId).DataTable({
                "ajax": "/ajax/users",
                sAjaxDataProp: 'users',
                columns: [
                    {
                    	data: 'userType',
                        "render": function ( data, type, row ) {
                            return row.userType === 'ADMIN' ? 'Admin' : 'Regular';
                        }
                    },
                    {
                        data: 'name'
                    },
                    {
                        data: 'login'
                    },
                    {
                        data: 'authenticatedCnt',
                        "render": function ( data, type, row ) {
                            return row.userType === 'ADMIN' ? '' : data;
                        }
                    },
                    {
                        data: null,
                        render: function(data) {
                        	return '<a href="#" onclick="Users.showUserInfo(' + data.id + ')" style="margin-right: 10px;">' +
                        				'<img src="/images/info.png" />' +
                        		   '</a>' + 
                        		   '<a href="#" onclick="Users.delUserConfirm(' + data.id + ')">' +
                        		   		'<img src="/images/delete.png" />' +
                        		   '</a>';
                        }
                    }
                ]
            });

		},

		delUserConfirm: function(userId) {
			var self = this;
			if (confirm("You are going to delete the user. Are you sure?")) {
	        	$.ajax({
	        		url: '/ajax/delUser?id=' + userId,
	                beforeSend: function() {
	                    $('#pleaseWaitDialog').modal();
	                },
	                success: function(data) {
	                    $("#statusMessage").addClass("alert alert-success");
	                    $("#statusMessage").text("User has been successfuly deleted");

	                    setTimeout(function() {
	                        $("#statusMessage").text('');
	                        $("#statusMessage").removeClass();
	                    }, 5000);

	                    $('#pleaseWaitDialog').modal('hide');
	                    usersTable.search( '' ).columns().search( '' ).draw();
	                    usersTable.ajax.reload();
	                },
	                error: function() {
	                    $('#pleaseWaitDialog').modal('hide');
	                }
	        	});
			}
		},
		
		showUserInfo: function(userId) {
			window.location = '/page/userInfo?id=' + userId;
		},
		
        fillUserInfo: function(user) {
            user.firstName = user.name.split(' ')[0];
            user.surname = user.name.split(' ')[1];
            delete user.name;
            
            Utils.fillForm(user);
        },

        delFeatures: function(userId) {
        	$.ajax({
        		url: '/ajax/delFeatures?id=' + userId,
                beforeSend: function() {
                    $('#pleaseWaitDialog').modal();
                },
                success: function(data) {
                    $("#statusMessage").addClass("alert alert-success");
                    $("#statusMessage").text("User features have been deleted");

                    setTimeout(function() {
                        $("#statusMessage").text('');
                        $("#statusMessage").removeClass();
                    }, 5000);

                    $('#pleaseWaitDialog').modal('hide');
                },
                error: function() {
                    $('#pleaseWaitDialog').modal('hide');
                }
        	});
        },
        
        save: function() {
        	var self = this,
                params = $('#userDetailsForm').serialize();
            $.ajax({
                url: '/ajax/updateUserInfo?' + params,
                beforeSend: function() {
                    $('#pleaseWaitDialog').modal();
                },
                success: function(data) {
                    $("#statusMessage").addClass("alert alert-success");
                    $("#statusMessage").text("User has been successfuly updated");
                    
                    self.fillUserInfo(data.user);
                    
                    setTimeout(function() {
                        $("#statusMessage").text('');
                        $("#statusMessage").removeClass();
                    }, 5000);
                    
                    $('#pleaseWaitDialog').modal('hide');
                },
                error: function() {
                    $('#pleaseWaitDialog').modal('hide');
                }
            });
        }	
	};
})();