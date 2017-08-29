var Utils = (function() {
	return {
		fillForm: function(data) {
            $.each(data, function(name, val) {
                var $el = $('[name="' + name + '"]'),
                type = $el.attr('type');

                switch(type) {
                    case 'checkbox':
                        $el.attr('checked', 'checked');
                        break;
                    case 'radio':
                        $el.filter('[value="' + val + '"]').attr('checked', 'checked');
                        break;
                    default:
                        $el.val(val);
                }
            });
		}
	};
})();