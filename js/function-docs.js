$DJMain(() => {
	$DJ.class('function-title').click(function(){
		const code = $DJ(this).attr('section-code');
		$DJ.id('function-content-' + code).classToggle('show-section');
	});
	$DJ.class('scope-detail-title').click(function(){
		const code = $DJ(this).attr('section-code');
		$DJ.id('scope-detail-content-' + code).classToggle('show-section');
	});
});
