const djProjectForm = $DJ.id('project-form');
const djProjectName = $DJ.id('project-name');
const djCMDName = $DJ.id('cmd-name');
const djCMDTemplates = $DJ.id('cmd-templates');
const djMoreStuffToggle = $DJ.id('more-stuff-toggle');
const djMoreStuffContent = $DJ.id('more-stuff-content');

const djTemplateSections = $DJ.class('template-section');

function call(funcRef) {
	return () => { funcRef(); };
}

function refreshView() {
	const projectForm = djProjectForm.form();

	const args = [];
	projectForm.assets && args.push(projectForm.assets);
	projectForm.patch && args.push(projectForm.patch);
	projectForm.textures && args.push(projectForm.textures);
	projectForm.maps && args.push(projectForm.maps);
	projectForm.scm && args.push(projectForm.scm);
	projectForm.run && args.push(projectForm.run);

	djCMDName.each(function(){
		const n = projectForm.project;
		this.innerHTML = n.indexOf(' ') > 0 || n.length === 0 ? '"' + n + '"' : n;
	});

	djCMDTemplates.each(function(){
		this.innerHTML = args.join(' ');
	});

	djTemplateSections.classRemove('visible-section');
	if (args.length) {
		$DJ.class('section-all').classAdd('visible-section');
	}
	for (let x in args) {
		$DJ.class('section-' + args[x]).classAdd('visible-section');
	}
}

$DJMain(() => {
	djProjectName.focus(function() {
		this.setSelectionRange(0, this.value.length);
	});
	djProjectForm.change(call(refreshView));
	refreshView();
});
