var Sessions = (function() {

	return {
		
		del: function(userId, sessionId) {
			window.location = '/page/delSession?sessionId=' + sessionId + "&userId=" + userId;
		}

	};
})();