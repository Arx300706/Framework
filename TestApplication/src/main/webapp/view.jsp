<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>ModelAndView</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 32px;
            color: #222;
        }

        .container {
            max-width: 760px;
        }

        table {
            border-collapse: collapse;
            width: 100%;
            max-width: 420px;
            margin-bottom: 28px;
        }

        th, td {
            border: 1px solid #cfcfcf;
            padding: 10px;
            text-align: left;
        }

        th {
            background: #f4f4f4;
        }

        form {
            margin: 15px 0 32px;
        }

        label {
            font-weight: bold;
        }

        input {
            padding: 8px;
            width: 220px;
            margin-top: 4px;
        }

        .field {
            margin-bottom: 12px;
        }

        button {
            background-color: #007bff;
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 4px;
            cursor: pointer;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>${message}</h1>

        <table>
            <thead>
                <tr>
                    <th>Cle</th>
                    <th>Valeur</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>key1</td>
                    <td>${key1}</td>
                </tr>
                <tr>
                    <td>key2</td>
                    <td>${key2}</td>
                </tr>
            </tbody>
        </table>

        <h2>Faire un post :</h2>

        <form action="${pageContext.request.contextPath}/login" method="POST">
            <div class="field">
                <label for="numero">Numero :</label><br>
                <input type="number" id="numero" name="numero" value="4196" required>
            </div>

            <div class="field">
                <label for="nom">Nom :</label><br>
                <input type="text" id="nom" name="nom" value="Armando" required>
            </div>

            <button type="submit">Envoyer (POST)</button>
        </form>

        <h2>Faire un post (objet User) :</h2>

        <form action="${pageContext.request.contextPath}/param" method="POST">
            <div class="field">
                <label for="id">Id :</label><br>
                <input type="number" id="id" name="id" value="1" required>
            </div>

            <div class="field">
                <label for="name">Name :</label><br>
                <input type="text" id="name" name="name" value="mi" required>
            </div>

            <div class="field">
                <label for="email">Email :</label><br>
                <input type="email" id="email" name="email" value="mi@example.com" required>
            </div>

            <button type="submit">Envoyer (POST)</button>
        </form>
    </div>
</body>
</html>
