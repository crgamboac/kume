document.addEventListener("DOMContentLoaded", () => {
	const toastEl = document.getElementById("appToast");
	if (toastEl) {
		const t = new bootstrap.Toast(toastEl);
		t.show();
	}
});
