document.addEventListener("DOMContentLoaded", () => {
	/* -------------------- INGREDIENTES -------------------- */

	const ingSelect = document.getElementById("ingredientsSelect");
	const ingContainer = document.getElementById("ingredients-container");
	const addIngredientBtn = document.getElementById("addIngredientBtn");

	let ingredientIndex = 0;

	addIngredientBtn.addEventListener("click", () => {
		const selected = Array.from(ingSelect.selectedOptions);
		if (selected.length === 0) return;

		selected.forEach((opt) => {
			const id = opt.value; // ← ID correcto del SELECT
			const name = opt.text; // ← Nombre mostrado

			const div = document.createElement("div");
			div.className = "border rounded p-3 bg-white mt-3";

			div.innerHTML = `
                <label class="form-label">Ingrediente</label>
                <input type="text" class="form-control" value="${name}" disabled>

                <input type="hidden"
                       name="ingredients[${ingredientIndex}].ingredient.id"
                       value="${id}">

                <label class="form-label mt-2">Cantidad</label>
                <input type="number"
                       class="form-control"
                       name="ingredients[${ingredientIndex}].quantity">
            `;

			ingContainer.appendChild(div);
			ingredientIndex++;
		});
	});

	/* -------------------- PASOS -------------------- */

	const stepsContainer = document.getElementById("steps-container");
	const addStepBtn = document.getElementById("addStepBtn");

	let stepIndex = 1;

	addStepBtn.addEventListener("click", () => {
		const div = document.createElement("div");
		div.className = "border rounded p-3 bg-white mt-3";

		div.innerHTML = `
            <label class="form-label">Número de Paso</label>
            <input type="number" name="steps[${stepIndex}].stepNumber" class="form-control">

            <label class="form-label mt-2">Instrucción</label>
            <textarea name="steps[${stepIndex}].instruction" class="form-control"></textarea>
        `;

		stepsContainer.appendChild(div);
		stepIndex++;
	});

	/* -------------------- MULTIMEDIA -------------------- */

	const mediaContainer = document.getElementById("media-container");
	const addMediaBtn = document.getElementById("addMediaBtn");

	addMediaBtn.addEventListener("click", () => {
		const div = document.createElement("div");
		div.className = "media-item border rounded p-3 bg-white mt-3";

		div.innerHTML = `
    <div class="row g-3 align-items-center">
        <div class="col-md-6">
            <label class="form-label">Archivo</label>
            <input type="file" name="extraImages" class="form-control" />
        </div>
        <div class="col-md-6">
            <label class="form-label">Tipo</label>
            <select name="mediaTypes" class="form-select">
                <option value="IMAGE">Imagen</option>
                <option value="VIDEO">Video</option>
            </select>
        </div>
    </div>
`;

		mediaContainer.appendChild(div);
	});
});
