const djTemplateSections = $DJ.class('template-section');
const djProjectForm = $DJ.id('project-form');
const djProjectName = $DJ.id('project-name');

const djCMDName = $DJ.id('cmd-name');
const djCMDAssets = $DJ.id('cmd-assets');
const djCMDPatch = $DJ.id('cmd-patch');
const djCMDRun = $DJ.id('cmd-run');
const djCMDSCM = $DJ.id('cmd-scm');

function call(funcRef) {
	return () => { funcRef(); };
}

function refreshView() {

	const assets = [];
	const sections = [];
	const projectForm = djProjectForm.form();

	projectForm.maps && assets.push(projectForm.maps) && sections.push(projectForm.maps);
	projectForm.assets && assets.push(projectForm.assets) && sections.push(projectForm.assets);
	projectForm.textures && assets.push(projectForm.textures) && sections.push(projectForm.textures);
	projectForm.decohack && sections.push(projectForm.decohack);
	projectForm.run && sections.push(projectForm.run);
	projectForm.scm && sections.push(projectForm.scm);

	if (assets.length === 0)
		assets.push('base');

	djCMDName.each(function(){
		const n = projectForm.project;
		this.innerHTML = n.indexOf(' ') > 0 || n.length === 0
			? '"' + n + '"' : n;
	});
	djCMDAssets.each(function(){
		this.innerHTML = assets.join('-') + ' ';
	});
	djCMDPatch.each(function(){
		this.innerHTML = projectForm.decohack ? projectForm.decohack + ' ' : '';
	});
	djCMDRun.each(function(){
		this.innerHTML = projectForm.run ? projectForm.run + ' ' : '';
	});
	djCMDSCM.each(function(){
		this.innerHTML = projectForm.scm ? projectForm.scm + ' ' : '';
	});

	djTemplateSections.classRemove('visible-section');
	for (let x in sections) {
		$DJ.class('section-'+sections[x]).classAdd('visible-section');
	}
}

$DJMain(() => {
	djProjectName.focus(function() {
		this.setSelectionRange(0, this.value.length);
	});
	djProjectForm.change(call(refreshView));
	refreshView();
});
