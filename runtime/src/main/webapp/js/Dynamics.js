var Dynamics = (function() {

    var statistics = {
        password: [],
        additional: []
	};

    var learningTextExample = "pack my bags with five dozen liquor jugs";

	var SKIP_CODES = [
		13, // ENTER
		9  //  TAB
	];

	return {
		
		setKeyListeners: function(formName, inputNames) {
			for (var i = 0; i < inputNames.length; i++) {
                (function () {
                    var inputName = inputNames[i];

                    $("#" + formName + " :input[name=" + inputName + "]").keydown(function(event) {
                        var code;
                        if (SKIP_CODES.indexOf(event.keyCode) !== -1) {
                            return;
                        }
                        if(event.keyCode != 16) { // If the pressed key is anything other than SHIFT
                            var c = String.fromCharCode(event.keyCode);
                            // if(event.shiftKey){ // If the SHIFT key is down, return the ASCII code for the capital letter
                            //     code = event.keyCode;
                            // } else { // If the SHIFT key is not down, convert to the ASCII code for the lowecase letter
                            c = c.toLowerCase(c);
                            code = c.charCodeAt(0);
                            // }
                        }

                        var keydownData = {
                            action: 'press',
                            code: code,
                            time: event.timeStamp
                        };

                        if (inputName === 'password') {
                            statistics.password.push(keydownData);
                        } else {
                            statistics.additional.push(keydownData);
                        }
                    });

                    $("#" + formName + " :input[name=" + inputName + "]").keyup(function(event) {
                        var code;
                        if (SKIP_CODES.indexOf(event.keyCode) !== -1) {
                            return;
                        }
                        if(event.keyCode != 16){ // If the pressed key is anything other than SHIFT
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
                            code: code,
                            time: event.timeStamp
                        };
                        if (inputName === 'password') {
                            statistics.password.push(keyupData);
                        } else {
                            statistics.additional.push(keyupData);
                        }
                    });
                })();


			}
		},
		
		initStatistics: function() {
			statistics.password = [];
			statistics.additional = [];
		},
		
		getStatJson: function() {
			return JSON.stringify(statistics);
		},

		isEnoughLearningText: function(text) {
			return true;

            var learningTextWords = text.split(/ +/);
            var matchingWords = 0;
            $.each(learningTextWords, function (index, val) {
                if (learningTextExample.indexOf(val) !== -1) {
                    matchingWords++;
                }
            });

            var exampleWordCnt = learningTextExample.split(/ +/).length;
            if ( ((matchingWords * 100) / exampleWordCnt) < 80 ) {
				return false;
            }

            return true;
		},
		
		init: function() {
			this.initStatistics();
			this.setKeyListeners('registrationForm', [ 'firstName', 'surname', 'login', 'password', 'learningText' ]);
			this.setKeyListeners('authForm', [ 'login', 'password', 'learningText' ]);

			$('input, textarea').on('paste', function(event) {
				event.preventDefault();
			});

			var self = this;
			
			$('#registrationForm').submit(function( event ) {
			  event.preventDefault();

                var learningText = $(this).find("textarea[name='learningText']").val();
                if (!self.isEnoughLearningText(learningText)) {
                    $("#matchErrorAlert").fadeTo(2000, 500).slideUp(500, function(){
                        $("#matchErrorAlert").slideUp(500);
                    });
                    return;
				}

			  var name = $(this).find('[name=firstName]').val().trim();
			  var surname = $(this).find('[name=surname]').val().trim();
			  var login = $(this).find('[name=login]').val().trim();
			  var password = $(this).find('[name=password]').val().trim();
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
                    $('#pleaseWaitDialog').modal('hide');
                },
                error: function() {
                	console.error("Can not register user");
                	$('#pleaseWaitDialog').modal('hide');
                }
        	  });
        	  
        	  $(this).find("input[type=text], [type=password], textarea:not([readonly='readonly'])").val("");
        	  self.initStatistics();
			  
			});
			$('#authForm').submit(function(event) {
  			    event.preventDefault();

                var learningText = $(this).find("textarea[name='learningText']").val();
                if (!self.isEnoughLearningText(learningText)) {
                    $("#matchErrorAlert").fadeTo(2000, 500).slideUp(500, function(){
                        $("#matchErrorAlert").slideUp(500);
                    });
                    return;
                }

				  var login = $(this).find('[name=login]').val().trim();
				  var password = $(this).find('[name=password]').val().trim();
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
	                    var text = "Authentication passed successfuly";
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
	          	  
	        	  $(this).find("input[type=text], [type=password], textarea:not([readonly='readonly'])").val("");
	          	  self.initStatistics();
			});
		}

	};
})();