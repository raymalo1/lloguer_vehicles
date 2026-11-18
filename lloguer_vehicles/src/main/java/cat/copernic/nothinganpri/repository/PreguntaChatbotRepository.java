/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.nothinganpri.repository;

import cat.copernic.nothinganpri.document.PreguntaChatbotDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PreguntaChatbotRepository extends MongoRepository<PreguntaChatbotDocument, String> {
    
}