const group = $DJ.createGroups({
	projectForm: '#project-form',
	projectName: '#project-name',
	cmdName: '#cmd-name',
	cmdTemplates: '#cmd-templates',
	templateSections: '.template-section',
});

function call(funcRef) {
	return () => { funcRef(); };
}

function refreshView() {
	const form = group.projectForm.form();

	const args = [];
	form.assets && args.push(form.assets);
	form.patch && args.push(form.patch);
	form.textures && args.push(form.textures);
	form.maps && args.push(form.maps);
	form.scm && args.push(form.scm);
	form.run && args.push(form.run);

	group.cmdName.each(function(){
		const n = form.project;
		this.innerHTML = n.indexOf(' ') > 0 || n.length === 0 ? '"' + n + '"' : n;
	});

	group.cmdTemplates.each(function(){
		this.innerHTML = args.join(' ');
	});

	group.templateSections.classRemove('visible-section');
	if (args.length) {
		$DJ.class('section-all').classAdd('visible-section');
	}
	for (let x in args) {
		$DJ.class('section-' + args[x]).classAdd('visible-section');
	}
}

$DJMain(() => {
	group.projectName.focus(function() {
		this.setSelectionRange(0, this.value.length);
	});
	group.projectForm.change(call(refreshView));
	refreshView();
});
