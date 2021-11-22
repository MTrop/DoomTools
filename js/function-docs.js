$DJMain(() => {
	$DJ.class('function-title').click(function(){
		const code = $DJ(this).attr('section-code');
		$DJ.id('function-content-' + code).classToggle('show-section');
	});
});
