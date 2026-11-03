/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     31/10/2026 11:46:09                          */
/*==============================================================*/


drop table if exists CUENTAS;

drop table if exists MOVIMIENTOS;

drop table if exists MOVIMIENTO_TIPOS;

drop table if exists PERFILES;

drop table if exists USUARIOS;

drop table if exists USUARIO_TIPOS;

/*==============================================================*/
/* Table: CUENTAS                                               */
/*==============================================================*/
create table CUENTAS
(
   ID_CUENTA            varchar(30) not null,
   ID_EMPLEADO_CREADOR  int,
   FECHA_CREACION_CUENTA date,
   SALDO_CUENTA         double,
   primary key (ID_CUENTA)
);

alter table CUENTAS comment 'Unicamente entran aqu� los clientes.

El identif';

/*==============================================================*/
/* Table: MOVIMIENTOS                                           */
/*==============================================================*/
create table MOVIMIENTOS
(
   ID_MOVIMIENTO        int not null auto_increment,
   ID_TIPO_MOVIMIENTO   int,
   CUENTA_ORIGEN_MOVIMIENTO varchar(30),
   CUENTA_DESTINO_MOVIMIENTO varchar(30),
   FECHA_MOVIMIENTO     datetime,
   VALOR_MOVIMIENTO     double,
   primary key (ID_MOVIMIENTO)
);

/*==============================================================*/
/* Table: MOVIMIENTO_TIPOS                                      */
/*==============================================================*/
create table MOVIMIENTO_TIPOS
(
   ID_TIPO_MOVIMIENTO   int not null auto_increment,
   NOMBRE_TIPO_MOVIMIENTO varchar(20),
   DESCRIPCION_TIPO_MOVIMIENTO text,
   ACCION_TIPO_MOVIMIENTO varchar(8),
   primary key (ID_TIPO_MOVIMIENTO)
);

/*==============================================================*/
/* Table: PERFILES                                              */
/*==============================================================*/
create table PERFILES
(
   ID_PERFIL            varchar(30) not null,
   USERNAME_PERFIL      varchar(1024),
   PASSWORD_PERFIL      varchar(1024),
   primary key (ID_PERFIL),
   key AK_USERNAME_INDEX (USERNAME_PERFIL)
);

/*==============================================================*/
/* Table: USUARIOS                                              */
/*==============================================================*/
create table USUARIOS
(
   ID_USUARIO           varchar(30) not null,
   ID_TIPO_USUARIO      int,
   NOMBRE_USUARIO       varchar(1024),
   APELLIDO_USUARIO     varchar(1024),
   ESTADO_USUARIO       varchar(1024),
   DIRECCION_USUARIO    varchar(1024),
   DNI_USUARIO          varchar(20),
   primary key (ID_USUARIO),
   key AK_DNI_INDEX (DNI_USUARIO)
);

/*==============================================================*/
/* Table: USUARIO_TIPOS                                         */
/*==============================================================*/
create table USUARIO_TIPOS
(
   ID_TIPO_USUARIO      int not null auto_increment,
   NOMBRE_TIPO_USUARIO  varchar(1024),
   DESCRIPCION_TIPO_USUARIO text,
   ESTADO_TIPO_USUARIO  bool,
   primary key (ID_TIPO_USUARIO)
);

alter table CUENTAS add constraint FK_RELATIONSHIP_6 foreign key (ID_CUENTA)
      references USUARIOS (ID_USUARIO) on delete restrict on update restrict;

alter table MOVIMIENTOS add constraint FK_RELATIONSHIP_3 foreign key (CUENTA_ORIGEN_MOVIMIENTO)
      references USUARIOS (ID_USUARIO) on delete restrict on update restrict;

alter table MOVIMIENTOS add constraint FK_RELATIONSHIP_4 foreign key (CUENTA_DESTINO_MOVIMIENTO)
      references CUENTAS (ID_CUENTA) on delete restrict on update restrict;

alter table MOVIMIENTOS add constraint FK_RELATIONSHIP_8 foreign key (ID_TIPO_MOVIMIENTO)
      references MOVIMIENTO_TIPOS (ID_TIPO_MOVIMIENTO) on delete restrict on update restrict;

alter table PERFILES add constraint FK_RELATIONSHIP_7 foreign key (ID_PERFIL)
      references USUARIOS (ID_USUARIO) on delete restrict on update restrict;

alter table USUARIOS add constraint FK_RELATIONSHIP_5 foreign key (ID_TIPO_USUARIO)
      references USUARIO_TIPOS (ID_TIPO_USUARIO) on delete restrict on update restrict;

