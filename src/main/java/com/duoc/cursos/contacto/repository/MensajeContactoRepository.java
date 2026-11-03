package com.duoc.cursos.contacto.repository;

import com.duoc.cursos.contacto.model.MensajeContacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MensajeContactoRepository extends JpaRepository<MensajeContacto, Long> {
}
