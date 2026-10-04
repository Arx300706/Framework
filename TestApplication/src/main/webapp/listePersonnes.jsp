<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.app.model.Personne" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Liste des personnes</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 32px;
        }

        table {
            border-collapse: collapse;
            width: 100%;
            max-width: 700px;
        }

        th, td {
            border: 1px solid #cfcfcf;
            padding: 10px;
            text-align: left;
        }

        th {
            background: #f4f4f4;
        }
    </style>
</head>
<body>
    <h1><%= request.getAttribute("message") %></h1>
    <p>Total: <strong><%= request.getAttribute("total") %></strong></p>

    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Nom</th>
                <th>Age</th>
            </tr>
        </thead>
        <tbody>
            <%
                List<Personne> personnes = (List<Personne>) request.getAttribute("personnes");
                if (personnes != null) {
                    for (Personne personne : personnes) {
            %>
            <tr>
                <td><%= personne.getId() %></td>
                <td><%= personne.getNom() %></td>
                <td><%= personne.getAge() %></td>
            </tr>
            <%
                    }
                }
            %>
        </tbody>
    </table>
</body>
</html>
