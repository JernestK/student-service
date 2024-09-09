$(document).ready(function() {
    // Define a function to handle button clicks
    function handleClick(event) {
        const buttonId = event.target.id;
        var controllerPath, fragmentType, fragmentId, btnColor;

        switch (buttonId) {
            case "workspace":
                controllerPath = "/workspace";
                fragmentType = "upload";
                fragmentId = 1;
                btnColor = "btn-primary";
                history.pushState({ path: controllerPath }, '', controllerPath);
                $("#myModal").css("display", "block"); // Afficher le modal
                break;
            case "moodle":
                controllerPath = "/moodle";
                fragmentType = "upload";
                fragmentId = 1;
                btnColor = "btn-secondary";
                console.log("Classe 2 "+ btnColor);
                break;
            case "matricule":
                controllerPath = "/matricule";
                fragmentType = "upload";
                fragmentId = 2;
                btnColor = "btn-danger";
                break;
            case "gate":
                controllerPath = "/portail_captif";
                fragmentType = "upload";
                fragmentId = 3;
                btnColor = "btn-success";
                break;
            default:
                console.warn("Unknown button clicked:", buttonId);
                return;
        }
        // Stocker le chemin dans une variable globale
        window.controllerPath = controllerPath;

        // Changer la couleur du bouton Importer
        //$('#importButton').removeClass().addClass('btn ' + btnColor);



        //console.log($('#importButton').removeClass().addClass('btn ' + btnColor))
        // Mettre à jour l'URL du navigateur
        history.pushState({ path: controllerPath }, '', controllerPath);
        loadFragment(controllerPath, fragmentType, fragmentId);
    }

    $("#myModal").css("display", "none"); // Masquer le modal au début

    // Écouteur d'événement pour le bouton "Envoyer" du modal
    // Écouteur d'événement pour le bouton "Envoyer" du modal
    $("#submitGroupName").click(function() {
        const groupName = $("#groupName").val();
        if (groupName) {
            // Effectuer une requête AJAX pour envoyer le nom du groupe
            $.ajax({
                type: "POST",
                url: "/workspace/group-name", // URL de votre contrôleur
                data: {
                    groupName: groupName, // Envoyer le nom du groupe
                },
                success: function(response) {
                    // Gérer la réponse ici (par exemple, afficher un message de succès)
                    console.log("Données envoyées avec succès :", response);
                    $("#myModal").css("display", "none");
                    // Vous pouvez également mettre à jour l'interface utilisateur ici si nécessaire
                },
                error: function(xhr, status, error) {
                    console.error("Erreur lors de l'envoi des données :", error);
                }
            });
        } else {
            alert("Veuillez entrer un nom de groupe.");
        }
    });

    // Attach click event listener to both buttons
    $("#workspace, #moodle, #matricule, #gate").click(handleClick);

    $("#nameId").html(modifyHeader(window.controllerPath));

    // URL de téléchargement du PDF et Excel
    var urlPdf = window.controllerPath + "/download/pdf";
    var urlExcel = window.controllerPath + "/download/xlsx";

    // Sélectionner l'élément avec l'ID "dwlPdf"
    var $dwlPdfBtn = $("#dwlPdf");
    $dwlPdfBtn.attr("href", window.controllerPath + "/download/pdf" + window.controllerPath);
    $dwlPdfBtn.click(function () {
        history.pushState({ path:urlPdf}, '', urlPdf)})
    console.log("window.location.href " + window.location.href)

    // Sélectionner l'élément avec l'ID "dwlExcel"
    var $dwlExcelBtn = $("#dwlExcel");
    $dwlExcelBtn.attr("href", window.controllerPath + "/download/xlsx" + window.controllerPath);
    $dwlExcelBtn.click(function () {
            history.pushState({ path:urlExcel}, '', urlExcel)})

    var urlImport = window.controllerPath + "/download";
    $("#importButton").click(function () {
        const type = window.controllerPath.substring(1); // Utiliser le type correspondant au bouton cliqué
        console.log("type Js "+type)
        const formData = new FormData();
        formData.append('file', $('#fileInput')[0].files[0]); // Assurez-vous que l'input de fichier a cet ID
        formData.append('type', type);

        $.ajax({
            url: window.controllerPath + "/upload",
            type: "POST",
            data: formData,
            processData: false,
            contentType: false,
            success: function(data) {
                $('#content-area').html(data);
            },
            error: function(xhr, status, error) {
                console.error('Erreur lors du téléchargement du fichier : ' + error);
            }
        });
    });

    // Fonction qui charge le contenu dynamique.
    function loadFragment(controllerPath, type, id) {
        $.ajax({
            url: controllerPath + '/fragment/' + type + '/' + id,
            success: function(data) {
                $('#content-area').html(data);
            },
            error: function(xhr, status, error) {
                console.error('Erreur lors du chargement du fragment : ' + error);
            }
        });
    }
});

function modifyHeader(str) {
    if (!str || typeof str !== 'string') {
        return ''; // Retourne une chaîne vide si str n'est pas une chaîne valide
    }
    var chaine = "";
    if (str.includes("workspace") || str.includes("moodle"))
        return "Email Address [Required]";
    return str.charAt(0).toUpperCase() + str.slice(1);
}

