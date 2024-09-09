document.querySelector('form').addEventListener('submit', function(event) {
    event.preventDefault();
    const fileInput = document.getElementById('fileInput');
    let file = fileInput.files[0];

    if (file == null) {
        showModalMessage('Veuillez sélectionner un fichier.');
        return;
    }
    const fileType = file.type;

    // Vérifier si le fichier est de type Excel
    if (fileType !== 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' && fileType !== 'application/vnd.ms-excel') {
        showModalMessage('Le fichier sélectionné n\'est pas un fichier Excel.');
        return;
    }
// Obtenir le chemin du contrôleur à partir d'une variable globale ou d'un attribut de données
    const controllerPath = getControllerPath();

    // Envoyer la requête AJAX avec le bon chemin
    var formData = new FormData();
    formData.append('file', fileInput.files[0]);
    console.log("upl " + window.controllerPath);

    const type = window.controllerPath.substring(1); // Utiliser le type correspondant au bouton cliqué
    console.log("type Js "+type)

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
    // $.ajax({
    //     url: controllerPath + '/upload',
    //     type: 'POST',
    //     data: formData,
    //     processData: false,
    //     contentType: false,
    //     success: function(data) {
    //         $('#content-area').html(data);
    //         // Mettre à jour l'URL du navigateur
    //         history.pushState({ path: controllerPath + '/upload' }, '', controllerPath);
    //     },
    //     error: function(xhr, status, error) {
    //         console.error('Erreur lors du chargement du fichier : ' + error);
    //     }
    // });
});

// Fonction pour obtenir le chemin du contrôleur
function getControllerPath() {
    return window.controllerPath;
}

function showModalMessage(message) {
    document.getElementById('modal-message').textContent = message;
    document.getElementById('modal').style.display = 'block';
}

function closeModal() {
    document.getElementById('modal').style.display = 'none';
}
