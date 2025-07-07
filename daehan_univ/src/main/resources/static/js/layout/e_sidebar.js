
    const sidebar = document.getElementById("sidebar");
    const hamburger = document.getElementById("hamburger");
    hamburger.addEventListener("click", () => {
    sidebar.classList.toggle("show");
});

    const menuLinks = document.querySelectorAll("[data-menu]");
    menuLinks.forEach((menu) => {
    menu.addEventListener("click", (e) => {
        e.preventDefault();
        const key = menu.getAttribute("data-menu");
        const submenu = document.getElementById(`submenu-${key}`);

        document.querySelectorAll(".submenu").forEach((sm) => {
            if (sm !== submenu) sm.style.display = "none";
        });

        if (submenu) {
            submenu.style.display =
                submenu.style.display === "block" ? "none" : "block";
        }
    });
});
