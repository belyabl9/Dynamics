var Dynamics = (function() {

	var statistics = [];
	var currentId;
	
	var makeId = function() {
	    var text = "";
	    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	    for( var i=0; i < 5; i++ )
	        text += possible.charAt(Math.floor(Math.random() * possible.length));

	    return text;
	}
	
	return {
		
		setKeyListeners: function(formName, inputNames) {
			for (var i = 0; i < inputNames.length; i++) {
				var name = inputNames[i];
			    $("#" + formName + " input[name=" + name + "]").keydown(function(event){
			    
			      var code;
		    	  if(event.keyCode!=16){ // If the pressed key is anything other than SHIFT
		    	        var c = String.fromCharCode(event.keyCode);
		    	        if(event.shiftKey){ // If the SHIFT key is down, return the ASCII code for the capital letter
		    	            code = event.keyCode;
		    	        } else { // If the SHIFT key is not down, convert to the ASCII code for the lowecase letter
		    	            c = c.toLowerCase(c);
		    	            code = c.charCodeAt(0);
		    	        }
		    	  }
		    	  
			    	var keydownData = {
			        	action: 'press',
			        	entity: code,
			        	time: event.timeStamp
			        };
			        statistics[currentId].push(keydownData);
			        //console.log(keydownData);
			    });
			    
			    $("#" + formName + " input[name=" + name + "]").keyup(function(event){
				      var code;
			    	  if(event.keyCode!=16){ // If the pressed key is anything other than SHIFT
			    	        var c = String.fromCharCode(event.keyCode);
			    	        if(event.shiftKey){ // If the SHIFT key is down, return the ASCII code for the capital letter
			    	            code = event.keyCode;
			    	        } else { // If the SHIFT key is not down, convert to the ASCII code for the lowecase letter
			    	            c = c.toLowerCase(c);
			    	            code = c.charCodeAt(0);
			    	        }
			    	  }
			    	
			        var keyupData = {
			        	action: 'release',
			        	entity: code,
			        	time: event.timeStamp
			        };
			        statistics[currentId].push(keyupData);
			        //console.log(keyupData);
			    });
			}
		},
		
		initStatistics: function() {
			statistics = {}
			currentId = makeId();
			statistics[currentId] = []
		},
		
		getStatJson: function() {
			return JSON.stringify(statistics);
		},
		
		init: function() {
			this.initStatistics();
			this.setKeyListeners('registrationForm', [ 'firstName', 'surname', 'login', 'password', 'learningText' ]);
			this.setKeyListeners('authForm', [ 'login', 'password' ]);

			$('input, textarea').on('paste', function(event) {
				event.preventDefault();
			});

			var self = this;
			
			$('#registrationForm').submit(function( event ) {
			  event.preventDefault();
			  
			  var name = $(this).find('[name=firstName]').val();
			  var surname = $(this).find('[name=surname]').val();
			  var login = $(this).find('[name=login]').val();
			  var password = $(this).find('[name=password]').val();
			  
			  var stat = self.getStatJson();
			  
			  var registrationData = {
				  name: name,
				  surname: surname,
				  login: login,
				  password: password,
				  stat: stat
			  };
			  
        	  $.ajax({
        		url: '/ajax/browserUserReg',
        		method: 'POST',
        		data: registrationData,
                beforeSend: function() {
                    $('#pleaseWaitDialog').modal();
                },
                success: function(data) {
                	console.log(data);
                },
                error: function() {
                	console.error("Can not register user");
                	$('#pleaseWaitDialog').modal('hide');
                }
        	  });
        	  
        	  $(this).find("input[type=text], textarea").val("");
        	  self.initStatistics();
			  
			});
			$('#authForm').submit(function( event ) {
				  event.preventDefault();
				  
				  var login = $(this).find('[name=login]').val();
				  var password = $(this).find('[name=password]').val();
				  
				  var stat = self.getStatJson();
				  
				  var authData = {
					  login: login,
					  password: password,
					  stat: stat
				  };
				  
	        	  $.ajax({
	          		url: '/auth',
	          		method: 'POST',
	          		data: authData,
	                beforeSend: function() {
	                    $('#pleaseWaitDialog').modal();
	                },
	                  success: function(data) {
	                    $("#statusMessage").addClass("alert alert-success");
	                    var text = "Authentication successfuly passed";
	                    if (data.threshold) {
	                    	text += " (" + (data.threshold * 100) + "%)";
	                    }
	                    $("#statusMessage").text(text);

	                    setTimeout(function() {
	                        $("#statusMessage").text('');
	                        $("#statusMessage").removeClass();
	                    }, 5000);

	                    $('#pleaseWaitDialog').modal('hide');
	                  },
	                  error: function(data) {
	                	  console.log(data);
		            	var response = $.parseJSON(data.responseText);
	                    $("#statusMessage").addClass("alert alert-danger");
	                    var text = "Authentication failed";
	                    if (response.threshold) {
	                    	text += " (" + (response.threshold * 100) + "%)";
	                    }
	                    $("#statusMessage").text(text);

	                    setTimeout(function() {
	                        $("#statusMessage").text('');
	                        $("#statusMessage").removeClass();
	                    }, 5000);
		              	$('#pleaseWaitDialog').modal('hide');
	                  }
	          	  });
	          	  
	        	  $(this).find("input[type=text], textarea").val("");
	          	  self.initStatistics();
			});
		}

	};
})();